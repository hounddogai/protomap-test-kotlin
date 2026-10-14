package com.acme.transport.crewing.qualification.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class QualificationThresholdTest {
    @Test
    fun `starts in new`() {
        val item = newQualificationThreshold("code-455", "name-256")
        assertEquals(QualificationThresholdStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newQualificationThreshold("code-455", "name-256")
        try {
            item.moveTo(QualificationThresholdStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(QualificationThresholdStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newQualificationThreshold("code-455", "name-256")
        item.status = QualificationThresholdStatus.FAILED
        assertFails("move") { item.moveTo(QualificationThresholdStatus.NEW) }
    }
}
