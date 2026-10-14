package com.acme.transport.crewing.qualification.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class QualificationClaimTest {
    @Test
    fun `starts in created`() {
        val item = newQualificationClaim("code-515", "title-360")
        assertEquals(QualificationClaimStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newQualificationClaim("code-515", "title-360")
        try {
            item.moveTo(QualificationClaimStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(QualificationClaimStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newQualificationClaim("code-515", "title-360")
        item.status = QualificationClaimStatus.RETIRED
        assertFails("move") { item.moveTo(QualificationClaimStatus.CREATED) }
    }
}
