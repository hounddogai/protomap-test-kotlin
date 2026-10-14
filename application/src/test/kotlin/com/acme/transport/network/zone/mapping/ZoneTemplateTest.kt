package com.acme.transport.network.zone.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ZoneTemplateTest {
    @Test
    fun `starts in created`() {
        val item = newZoneTemplate("code-577", "name-159")
        assertEquals(ZoneTemplateStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newZoneTemplate("code-577", "name-159")
        try {
            item.moveTo(ZoneTemplateStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ZoneTemplateStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newZoneTemplate("code-577", "name-159")
        item.status = ZoneTemplateStatus.RETIRED
        assertFails("move") { item.moveTo(ZoneTemplateStatus.CREATED) }
    }
}
