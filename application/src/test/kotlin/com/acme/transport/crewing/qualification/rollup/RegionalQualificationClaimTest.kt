package com.acme.transport.crewing.qualification.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalQualificationClaimTest {
    @Test
    fun `starts in requested`() {
        val item = newRegionalQualificationClaim("code-318", "name-296")
        assertEquals(RegionalQualificationClaimStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newRegionalQualificationClaim("code-318", "name-296")
        try {
            item.moveTo(RegionalQualificationClaimStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalQualificationClaimStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRegionalQualificationClaim("code-318", "name-296")
        item.status = RegionalQualificationClaimStatus.EXPIRED
        assertFails("move") { item.moveTo(RegionalQualificationClaimStatus.REQUESTED) }
    }
}
