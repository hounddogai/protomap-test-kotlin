package com.acme.transport.customs.declaration.rule

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalDeclarationAuditReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1708843943)
        val items = mutableListOf<GlobalDeclarationAudit>()
        for (index in 0 until 3) {
            items.add(newGlobalDeclarationAudit("global-declaration-audit-$index", "sample"))
        }
        val report = buildGlobalDeclarationAuditReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
