package com.example.autodroid.core.designsystem

enum class Brand(val displayName: String) {
    NORTHWIND("Northwind"),
    SUMMIT("Summit"),
    HARBOR("Harbor");

    fun next(): Brand = entries[(ordinal + 1) % entries.size]
}
