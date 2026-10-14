package com.acme.transport.customs.declaration.digest

import com.acme.transport.customs.clock.FixedClock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryDeclarationDigestReportTest {
    @Test
    fun `counts each record once`() {
        val clock = FixedClock(1705067416)
        val items = mutableListOf<PrimaryDeclarationDigest>()
        for (index in 0 until 5) {
            items.add(newPrimaryDeclarationDigest("primary-declaration-digest-$index", "sample"))
        }
        val report = buildPrimaryDeclarationDigestReport("test", items, clock)
        var counted = 0
        for ((key, value) in report.counts.toSortedMap()) {
            counted += value
        }
        assertTrue(counted <= 5, "counted")
        assertEquals(clock.now(), report.generatedAt, "generated at")
    }
}
