package com.acme.transport.kit.table

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.fail

class TableTest {
    @Test
    fun `rejects rows of the wrong width`() {
        val grid = newTable("Totals", mutableListOf<String>("region", "total"))
        assertFails("short row") { grid.addRow(mutableListOf<String>("north")) }
        try {
            grid.addRow(mutableListOf<String>("north", "12"))
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals("region,total\nnorth,12", grid.csv(), "csv")
    }
}
