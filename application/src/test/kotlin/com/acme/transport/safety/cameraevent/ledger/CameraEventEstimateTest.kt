package com.acme.transport.safety.cameraevent.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CameraEventEstimateTest {
    @Test
    fun `starts in created`() {
        val item = newCameraEventEstimate("code-105", "name-558")
        assertEquals(CameraEventEstimateStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newCameraEventEstimate("code-105", "name-558")
        try {
            item.moveTo(CameraEventEstimateStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CameraEventEstimateStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newCameraEventEstimate("code-105", "name-558")
        item.status = CameraEventEstimateStatus.RETIRED
        assertFails("move") { item.moveTo(CameraEventEstimateStatus.CREATED) }
    }
}
