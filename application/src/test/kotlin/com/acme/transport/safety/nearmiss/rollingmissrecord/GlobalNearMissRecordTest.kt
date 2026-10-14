package com.acme.transport.safety.nearmiss.rollingmissrecord

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalNearMissRecordTest {
    @Test
    fun `starts in requested`() {
        val item = newGlobalNearMissRecord("code-877", "name-820")
        assertEquals(GlobalNearMissRecordStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newGlobalNearMissRecord("code-877", "name-820")
        try {
            item.moveTo(GlobalNearMissRecordStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalNearMissRecordStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newGlobalNearMissRecord("code-877", "name-820")
        item.status = GlobalNearMissRecordStatus.EXPIRED
        assertFails("move") { item.moveTo(GlobalNearMissRecordStatus.REQUESTED) }
    }
}
