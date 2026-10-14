package com.acme.transport.customs.hold.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedHoldRequestTest {
    @Test
    fun `starts in created`() {
        val item = newCappedHoldRequest("code-264", "title-970")
        assertEquals(CappedHoldRequestStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newCappedHoldRequest("code-264", "title-970")
        try {
            item.moveTo(CappedHoldRequestStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedHoldRequestStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newCappedHoldRequest("code-264", "title-970")
        item.status = CappedHoldRequestStatus.RETIRED
        assertFails("move") { item.moveTo(CappedHoldRequestStatus.CREATED) }
    }
}
