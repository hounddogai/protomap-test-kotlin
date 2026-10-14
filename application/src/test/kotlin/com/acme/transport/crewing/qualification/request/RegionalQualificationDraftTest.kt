package com.acme.transport.crewing.qualification.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalQualificationDraftTest {
    @Test
    fun `starts in proposed`() {
        val item = newRegionalQualificationDraft("code-522", "name-550")
        assertEquals(RegionalQualificationDraftStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newRegionalQualificationDraft("code-522", "name-550")
        try {
            item.moveTo(RegionalQualificationDraftStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalQualificationDraftStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newRegionalQualificationDraft("code-522", "name-550")
        item.status = RegionalQualificationDraftStatus.WITHDRAWN
        assertFails("move") { item.moveTo(RegionalQualificationDraftStatus.PROPOSED) }
    }
}
