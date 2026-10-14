package com.acme.transport.crewing.qualification.qualificationassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveQualificationAssignmentTest {
    @Test
    fun `starts in idle`() {
        val item = newAdaptiveQualificationAssignment("code-641", "title-795")
        assertEquals(AdaptiveQualificationAssignmentStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newAdaptiveQualificationAssignment("code-641", "title-795")
        try {
            item.moveTo(AdaptiveQualificationAssignmentStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveQualificationAssignmentStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newAdaptiveQualificationAssignment("code-641", "title-795")
        item.status = AdaptiveQualificationAssignmentStatus.STOPPED
        assertFails("move") { item.moveTo(AdaptiveQualificationAssignmentStatus.IDLE) }
    }
}
