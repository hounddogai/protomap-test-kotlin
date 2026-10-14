package com.acme.transport.network.zone.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ZoneMappingTest {
    @Test
    fun `starts in created`() {
        val item = newZoneMapping("code-413", "name-609")
        assertEquals(ZoneMappingStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newZoneMapping("code-413", "name-609")
        try {
            item.moveTo(ZoneMappingStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ZoneMappingStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newZoneMapping("code-413", "name-609")
        item.status = ZoneMappingStatus.RETIRED
        assertFails("move") { item.moveTo(ZoneMappingStatus.CREATED) }
    }
}
