package com.acme.transport.crewing.qualification.qualificationmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualQualificationThresholdTest {
    @Test
    fun `starts in idle`() {
        val item = newManualQualificationThreshold("code-384", "name-90")
        assertEquals(ManualQualificationThresholdStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newManualQualificationThreshold("code-384", "name-90")
        try {
            item.moveTo(ManualQualificationThresholdStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualQualificationThresholdStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newManualQualificationThreshold("code-384", "name-90")
        item.status = ManualQualificationThresholdStatus.STOPPED
        assertFails("move") { item.moveTo(ManualQualificationThresholdStatus.IDLE) }
    }
}
