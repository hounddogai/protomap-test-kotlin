package com.acme.transport.network.zone.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ZoneRuleTest {
    @Test
    fun `starts in open`() {
        val item = newZoneRule("code-970", "title-449")
        assertEquals(ZoneRuleStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newZoneRule("code-970", "title-449")
        try {
            item.moveTo(ZoneRuleStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ZoneRuleStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newZoneRule("code-970", "title-449")
        item.status = ZoneRuleStatus.ARCHIVED
        assertFails("move") { item.moveTo(ZoneRuleStatus.OPEN) }
    }
}
