package com.acme.transport.customs.hold.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class HoldWindowTest {
    @Test
    fun `starts in created`() {
        val item = newHoldWindow("code-273", "name-625")
        assertEquals(HoldWindowStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newHoldWindow("code-273", "name-625")
        try {
            item.moveTo(HoldWindowStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(HoldWindowStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newHoldWindow("code-273", "name-625")
        item.status = HoldWindowStatus.RETIRED
        assertFails("move") { item.moveTo(HoldWindowStatus.CREATED) }
    }
}
