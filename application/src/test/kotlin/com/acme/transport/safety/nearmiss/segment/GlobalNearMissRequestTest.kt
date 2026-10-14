package com.acme.transport.safety.nearmiss.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalNearMissRequestTest {
    @Test
    fun `starts in idle`() {
        val item = newGlobalNearMissRequest("code-892", "title-560")
        assertEquals(GlobalNearMissRequestStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newGlobalNearMissRequest("code-892", "title-560")
        try {
            item.moveTo(GlobalNearMissRequestStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalNearMissRequestStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newGlobalNearMissRequest("code-892", "title-560")
        item.status = GlobalNearMissRequestStatus.STOPPED
        assertFails("move") { item.moveTo(GlobalNearMissRequestStatus.IDLE) }
    }
}
