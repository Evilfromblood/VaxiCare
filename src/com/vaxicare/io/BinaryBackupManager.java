package com.vaxicare.io;

import com.vaxicare.model.DoseRecord;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages low-level binary serialization and deserialization of dose records.
 * Demonstrates Unit IV: Byte streams (FileOutputStream, FileInputStream), ByteBuffer manipulation,
 * manual UTF-8 byte conversions (strictly avoiding Character streams or ObjectOutputStream),
 * and nested try-catch-finally exception handling.
 */
public class BinaryBackupManager {

    private static final int MAGIC_HEADER = 0x56415843; // "VAXC" in hex

    /**
     * Writes all DoseRecord objects into a binary file using ONLY FileOutputStream and ByteBuffer.
     */
    public void writeBackup(List<DoseRecord> records, String filePath) throws IOException {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(filePath);

            // 1. Write 4-byte Magic Header
            byte[] headerBuf = ByteBuffer.allocate(4).putInt(MAGIC_HEADER).array();
            fos.write(headerBuf);

            // 2. Write 4-byte Record Count
            int total = (records != null) ? records.size() : 0;
            byte[] countBuf = ByteBuffer.allocate(4).putInt(total).array();
            fos.write(countBuf);

            // 3. Write each DoseRecord manually using ByteBuffer
            if (records != null) {
                for (DoseRecord r : records) {
                    // doseId (int, 4 bytes)
                    writeInt(fos, r.getDoseId());

                    // benId (String: length + UTF-8 bytes)
                    writeString(fos, r.getBenId());

                    // vaccineName (String: length + UTF-8 bytes)
                    writeString(fos, r.getVaccineName());

                    // doseNo (int, 4 bytes)
                    writeInt(fos, r.getDoseNo());

                    // doseDate (String: length + UTF-8 bytes)
                    writeString(fos, r.getDoseDate());

                    // nextDue (String: length + UTF-8 bytes)
                    writeString(fos, r.getNextDue());

                    // certificateNo (String: length + UTF-8 bytes)
                    writeString(fos, r.getCertificateNo());
                }
            }
            fos.flush();
            System.out.println("[BinaryBackupManager] Successfully wrote " + total + " records to " + filePath);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    System.err.println("[BinaryBackupManager] Error closing FileOutputStream: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Reads all DoseRecord objects back from the binary file using ONLY FileInputStream and ByteBuffer.
     * Implements explicit nested try-catch-finally blocks as required by the syllabus and specifications.
     */
    public List<DoseRecord> restoreFromBinary(String filePath) throws IOException {
        FileInputStream fis = null;
        List<DoseRecord> restoredList = new ArrayList<>();

        // OUTER try block
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                throw new FileNotFoundException("Binary backup file not found: " + filePath);
            }
            fis = new FileInputStream(file);

            // NESTED INNER try block
            try {
                // Read and verify 4-byte magic header
                byte[] headerBuf = new byte[4];
                readFully(fis, headerBuf);
                int magic = ByteBuffer.wrap(headerBuf).getInt();
                if (magic != MAGIC_HEADER) {
                    throw new IOException("Corrupted backup file: Invalid magic header 0x" + Integer.toHexString(magic));
                }

                // Read 4-byte record count
                byte[] countBuf = new byte[4];
                readFully(fis, countBuf);
                int totalRecords = ByteBuffer.wrap(countBuf).getInt();

                for (int i = 0; i < totalRecords; i++) {
                    int doseId = readInt(fis);
                    String benId = readString(fis);
                    String vaccineName = readString(fis);
                    int doseNo = readInt(fis);
                    String doseDate = readString(fis);
                    String nextDue = readString(fis);
                    String certificateNo = readString(fis);

                    restoredList.add(new DoseRecord(doseId, benId, vaccineName, doseNo, doseDate, nextDue, certificateNo));
                }

            } catch (IOException innerEx) {
                System.err.println("[BinaryBackupManager] Nested inner catch: Deserialization failed: " + innerEx.getMessage());
                throw innerEx;
            } finally {
                System.out.println("[BinaryBackupManager] Nested inner finally: Stream decoding block finished. Records decoded: " + restoredList.size());
            }

        } catch (FileNotFoundException outerFnf) {
            System.err.println("[BinaryBackupManager] Outer catch: Target file could not be opened: " + outerFnf.getMessage());
            throw outerFnf;
        } catch (IOException outerIo) {
            System.err.println("[BinaryBackupManager] Outer catch: I/O stream failure: " + outerIo.getMessage());
            throw outerIo;
        } finally {
            // OUTER finally block for stream closure
            if (fis != null) {
                try {
                    fis.close();
                    System.out.println("[BinaryBackupManager] Outer finally: FileInputStream closed successfully.");
                } catch (IOException closeEx) {
                    System.err.println("[BinaryBackupManager] Outer finally: Error closing FileInputStream: " + closeEx.getMessage());
                }
            }
        }

        return restoredList;
    }

    /**
     * Verifies that the count of restored binary records precisely matches the SQLite database record count.
     */
    public boolean verifyRestoredCount(int sqliteCount, int restoredCount) {
        boolean match = (sqliteCount == restoredCount);
        System.out.println(String.format("[BinaryBackupManager] Verification Check -> SQLite Count: %d | Restored Binary Count: %d | MATCH: %s",
                sqliteCount, restoredCount, match));
        return match;
    }

    // Low-level helper methods using ByteBuffer

    private void writeInt(FileOutputStream fos, int value) throws IOException {
        byte[] buf = ByteBuffer.allocate(4).putInt(value).array();
        fos.write(buf);
    }

    private int readInt(FileInputStream fis) throws IOException {
        byte[] buf = new byte[4];
        readFully(fis, buf);
        return ByteBuffer.wrap(buf).getInt();
    }

    private void writeString(FileOutputStream fos, String str) throws IOException {
        byte[] utfBytes = (str != null) ? str.getBytes(StandardCharsets.UTF_8) : new byte[0];
        writeInt(fos, utfBytes.length);
        if (utfBytes.length > 0) {
            fos.write(utfBytes);
        }
    }

    private String readString(FileInputStream fis) throws IOException {
        int length = readInt(fis);
        if (length <= 0) {
            return "";
        }
        byte[] utfBytes = new byte[length];
        readFully(fis, utfBytes);
        return new String(utfBytes, StandardCharsets.UTF_8);
    }

    private void readFully(FileInputStream fis, byte[] b) throws IOException {
        int offset = 0;
        int remaining = b.length;
        while (remaining > 0) {
            int read = fis.read(b, offset, remaining);
            if (read == -1) {
                throw new IOException("Unexpected end of file while reading binary stream. Expected " + b.length + " bytes, read " + offset);
            }
            offset += read;
            remaining -= read;
        }
    }
}
