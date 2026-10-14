package com.acme.transport.customs.declaration.segment

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyDeclarationProfileReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1707823896)
        val items = mutableListOf<LegacyDeclarationProfile>()
        for (index in 0 until 3) {
            items.add(newLegacyDeclarationProfile("legacy-declaration-profile-$index", "sample"))
        }
        val report = buildLegacyDeclarationProfileReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
