package com.acme.transport.customs.hold.adaptiveholdmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryHoldMappingTest {
    @Test
    fun `starts in new`() {
        val item = newPrimaryHoldMapping("code-267", "label-877")
        assertEquals(PrimaryHoldMappingStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newPrimaryHoldMapping("code-267", "label-877")
        try {
            item.moveTo(PrimaryHoldMappingStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryHoldMappingStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newPrimaryHoldMapping("code-267", "label-877")
        item.status = PrimaryHoldMappingStatus.FAILED
        assertFails("move") { item.moveTo(PrimaryHoldMappingStatus.NEW) }
    }
}
