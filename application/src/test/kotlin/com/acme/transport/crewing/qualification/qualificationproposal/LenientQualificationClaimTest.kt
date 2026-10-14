package com.acme.transport.crewing.qualification.qualificationproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientQualificationClaimTest {
    @Test
    fun `starts in open`() {
        val item = newLenientQualificationClaim("code-250", "name-834")
        assertEquals(LenientQualificationClaimStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newLenientQualificationClaim("code-250", "name-834")
        try {
            item.moveTo(LenientQualificationClaimStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientQualificationClaimStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newLenientQualificationClaim("code-250", "name-834")
        item.status = LenientQualificationClaimStatus.ARCHIVED
        assertFails("move") { item.moveTo(LenientQualificationClaimStatus.OPEN) }
    }
}
