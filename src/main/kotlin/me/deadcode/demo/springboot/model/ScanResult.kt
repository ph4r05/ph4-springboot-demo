package me.deadcode.demo.springboot.model

enum class ScanResult(val r: Int) {
    WAITING(-1),
    QUEUED(-2),
    SCANNING(-3),
    SCANNED_OK(1),
    SCANNED_FAIL(2),
    SCANNED_ERROR(3);

    companion object {
        fun fromInt(value: Int): ScanResult? = values().find { it.r == value }
    }
}