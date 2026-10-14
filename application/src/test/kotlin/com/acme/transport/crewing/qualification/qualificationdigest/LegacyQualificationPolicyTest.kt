package com.acme.transport.crewing.qualification.qualificationdigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyQualificationPolicyTest {
    @Test
    fun `starts in requested`() {
        val item = newLegacyQualificationPolicy("code-919", "name-260")
        assertEquals(LegacyQualificationPolicyStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newLegacyQualificationPolicy("code-919", "name-260")
        try {
            item.moveTo(LegacyQualificationPolicyStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyQualificationPolicyStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newLegacyQualificationPolicy("code-919", "name-260")
        item.status = LegacyQualificationPolicyStatus.EXPIRED
        assertFails("move") { item.moveTo(LegacyQualificationPolicyStatus.REQUESTED) }
    }
}
