package com.acme.transport.crewing.qualification.qualificationpreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualQualificationProfileTest {
    @Test
    fun `starts in proposed`() {
        val item = newManualQualificationProfile("code-598", "title-229")
        assertEquals(ManualQualificationProfileStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newManualQualificationProfile("code-598", "title-229")
        try {
            item.moveTo(ManualQualificationProfileStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualQualificationProfileStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newManualQualificationProfile("code-598", "title-229")
        item.status = ManualQualificationProfileStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ManualQualificationProfileStatus.PROPOSED) }
    }
}
