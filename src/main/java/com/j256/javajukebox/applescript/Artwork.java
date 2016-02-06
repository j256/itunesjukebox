package com.j256.javajukebox.applescript;

/**
 * Artwork from the track.
 * 
 * @author graywatson
 */
public class Artwork {

	private final String ext;
	private final ImageType type;
	private final byte[] bytes;

	public Artwork(String ext, byte[] bytes) {
		this.ext = ext;
		this.type = ImageType.fromExt(ext);
		this.bytes = bytes;
	}

	public String getExt() {
		return ext;
	}

	public ImageType getType() {
		return type;
	}

	public byte[] getBytes() {
		return bytes;
	}

	public static enum ImageType {
		PNG("png", "image/png"),
		JPEG("png", "image/" + "jpeg"),
		// end
		;

		private String ext;
		private String mimeType;

		private ImageType(String ext, String mimeType) {
			this.ext = ext;
			this.mimeType = mimeType;
		}

		public static ImageType fromExt(String ext) {
			for (ImageType type : values()) {
				if (type.ext.equals(ext)) {
					return type;
				}
			}
			return null;
		}

		public String getExt() {
			return ext;
		}

		public String getMimeType() {
			return mimeType;
		}
	}
}
