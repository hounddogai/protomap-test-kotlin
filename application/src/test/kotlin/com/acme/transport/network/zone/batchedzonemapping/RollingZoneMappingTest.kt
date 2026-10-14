package com.acme.transport.network.zone.batchedzonemapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingZoneMappingTest {
    @Test
    fun `starts in new`() {
        val item = newRollingZoneMapping("code-505", "label-162")
        assertEquals(RollingZoneMappingStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newRollingZoneMapping("code-505", "label-162")
        try {
            item.moveTo(RollingZoneMappingStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingZoneMappingStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRollingZoneMapping("code-505", "label-162")
        item.status = RollingZoneMappingStatus.FAILED
        assertFails("move") { item.moveTo(RollingZoneMappingStatus.NEW) }
    }
}
