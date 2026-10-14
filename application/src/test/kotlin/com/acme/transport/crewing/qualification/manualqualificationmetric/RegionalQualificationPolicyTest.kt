package com.acme.transport.crewing.qualification.manualqualificationmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalQualificationPolicyTest {
    @Test
    fun `starts in new`() {
        val item = newRegionalQualificationPolicy("code-405", "name-537")
        assertEquals(RegionalQualificationPolicyStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newRegionalQualificationPolicy("code-405", "name-537")
        try {
            item.moveTo(RegionalQualificationPolicyStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalQualificationPolicyStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRegionalQualificationPolicy("code-405", "name-537")
        item.status = RegionalQualificationPolicyStatus.FAILED
        assertFails("move") { item.moveTo(RegionalQualificationPolicyStatus.NEW) }
    }
}
