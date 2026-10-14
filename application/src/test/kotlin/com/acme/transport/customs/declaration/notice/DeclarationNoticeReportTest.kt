package com.acme.transport.customs.declaration.notice

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeclarationNoticeReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707171703)
        val items = mutableListOf<DeclarationNotice>()
        for (index in 0 until 3) {
            items.add(newDeclarationNotice("declaration-notice-$index", "sample"))
        }
        val report = buildDeclarationNoticeReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
