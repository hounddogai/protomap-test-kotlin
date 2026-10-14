package com.acme.transport.emissions.idling.idlingmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IdlingRecordTest {
    @Test
    fun `starts in created`() {
        val item = newIdlingRecord("code-195", "label-19")
        assertEquals(IdlingRecordStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newIdlingRecord("code-195", "label-19")
        try {
            item.moveTo(IdlingRecordStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IdlingRecordStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newIdlingRecord("code-195", "label-19")
        item.status = IdlingRecordStatus.RETIRED
        assertFails("move") { item.moveTo(IdlingRecordStatus.CREATED) }
    }
}
