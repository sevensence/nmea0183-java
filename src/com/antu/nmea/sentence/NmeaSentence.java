package com.antu.nmea.sentence;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.antu.nmea.annotation.SentenceField;
import com.antu.nmea.codec.AbstractNmeaSentenceCodec;

public abstract class NmeaSentence implements INmeaSentence {
	
	private Date receiveDate;
	
	public NmeaSentence() {
		this.receiveDate = Calendar.getInstance().getTime();
	}
	
	public NmeaSentence(Date date) {
		this.receiveDate = date;
		
		if (this.receiveDate == null)
			this.receiveDate = Calendar.getInstance().getTime();
	}
	
	public NmeaSentence(long currentTimeSinceEpochInSeconds) {
		
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(currentTimeSinceEpochInSeconds * 1000);
		
		this.receiveDate = cal.getTime();
	}

	abstract public String sentenceType();

	@Override
	public Date getReceiveDate() {
		return receiveDate;
	}
	
	static public void print(NmeaSentence sentence) {
		
		List<Field> fields = AbstractNmeaSentenceCodec.getSentenceFields((NmeaSentence)sentence);
		
		for (Field field : fields) {
			try {
				System.out.println(field.getName() + ": " + field.get(sentence).toString());
			} catch (IllegalArgumentException
					| IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(this.sentenceType()).append("\n");
		
		List<Field> sentenceFields = AbstractNmeaSentenceCodec.getSentenceFields(this);
		
		try {
			for (Field field : sentenceFields) {
				SentenceField annotation = (SentenceField)field.getAnnotation(SentenceField.class);
				
				if (!annotation.isIgnoredInReconstruction()) {
					sb.append(field.getName()).append(": ");
						sb.append(field.get(this).toString());
					sb.append("\n");
				}
			}
			
			List<Field> messageFields = AbstractNmeaSentenceCodec.getMessageFields(this);
			for (Field field : messageFields) {
				sb.append(field.getName()).append(": ");
				sb.append(field.get(this).toString());
				sb.append("\n");
			}
			
			if (this instanceof EncapsulationSentence) {
				IEncapsulatedSentence encapsulated = ((EncapsulationSentence)this).getEncapsulatedSentence();
				
				if (encapsulated != null) {
					List<Field> embeddedFields = AbstractNmeaSentenceCodec.getMessageFields(encapsulated);
					
					for (Field eField : embeddedFields) {
						sb.append(eField.getName()).append(": ");
						sb.append(eField.get(encapsulated).toString());
						sb.append("\n");
					}
				}
			}
			
		} catch (IllegalArgumentException | IllegalAccessException e) {
		}
		
		return sb.toString();
	}
}