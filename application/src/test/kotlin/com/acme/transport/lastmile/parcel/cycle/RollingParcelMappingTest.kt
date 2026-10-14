package com.acme.transport.lastmile.parcel.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingParcelMappingTest {
    @Test
    fun `starts in new`() {
        val item = newRollingParcelMapping("code-600", "title-180")
        assertEquals(RollingParcelMappingStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newRollingParcelMapping("code-600", "title-180")
        try {
            item.moveTo(RollingParcelMappingStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingParcelMappingStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRollingParcelMapping("code-600", "title-180")
        item.status = RollingParcelMappingStatus.FAILED
        assertFails("move") { item.moveTo(RollingParcelMappingStatus.NEW) }
    }
}
