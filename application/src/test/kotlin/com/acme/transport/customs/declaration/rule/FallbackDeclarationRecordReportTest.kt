package com.acme.transport.customs.declaration.rule

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackDeclarationRecordReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707562405)
        val items = mutableListOf<FallbackDeclarationRecord>()
        for (index in 0 until 5) {
            items.add(newFallbackDeclarationRecord("fallback-declaration-record-$index", "sample"))
        }
        val report = buildFallbackDeclarationRecordReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
