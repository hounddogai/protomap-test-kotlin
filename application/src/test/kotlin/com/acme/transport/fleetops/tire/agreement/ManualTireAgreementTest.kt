package com.acme.transport.fleetops.tire.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualTireAgreementTest {
    @Test
    fun `starts in created`() {
        val item = newManualTireAgreement("code-988", "label-301")
        assertEquals(ManualTireAgreementStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newManualTireAgreement("code-988", "label-301")
        try {
            item.moveTo(ManualTireAgreementStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualTireAgreementStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newManualTireAgreement("code-988", "label-301")
        item.status = ManualTireAgreementStatus.RETIRED
        assertFails("move") { item.moveTo(ManualTireAgreementStatus.CREATED) }
    }
}
