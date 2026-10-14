package com.acme.transport.fleetops.defect.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefectRequestTest {
    @Test
    fun `starts in proposed`() {
        val item = newDefectRequest("code-801", "name-662")
        assertEquals(DefectRequestStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newDefectRequest("code-801", "name-662")
        try {
            item.moveTo(DefectRequestStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefectRequestStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newDefectRequest("code-801", "name-662")
        item.status = DefectRequestStatus.WITHDRAWN
        assertFails("move") { item.moveTo(DefectRequestStatus.PROPOSED) }
    }
}
