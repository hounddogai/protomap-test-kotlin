package com.acme.transport.crewing.qualification.qualificationpreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualQualificationTargetTest {
    @Test
    fun `starts in proposed`() {
        val item = newManualQualificationTarget("code-467", "label-439")
        assertEquals(ManualQualificationTargetStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newManualQualificationTarget("code-467", "label-439")
        try {
            item.moveTo(ManualQualificationTargetStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualQualificationTargetStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newManualQualificationTarget("code-467", "label-439")
        item.status = ManualQualificationTargetStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ManualQualificationTargetStatus.PROPOSED) }
    }
}
