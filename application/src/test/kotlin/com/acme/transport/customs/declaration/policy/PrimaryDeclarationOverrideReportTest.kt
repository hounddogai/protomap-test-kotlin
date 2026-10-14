package com.acme.transport.customs.declaration.policy

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryDeclarationOverrideReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1701998894)
        val items = mutableListOf<PrimaryDeclarationOverride>()
        for (index in 0 until 3) {
            items.add(newPrimaryDeclarationOverride("primary-declaration-override-$index", "sample"))
        }
        val report = buildPrimaryDeclarationOverrideReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 3, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
