package com.acme.transport.emissions.emissionfactor.factorpreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingEmissionFactorAgreementTest {
    @Test
    fun `starts in created`() {
        val item = newRollingEmissionFactorAgreement("code-859", "label-25")
        assertEquals(RollingEmissionFactorAgreementStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newRollingEmissionFactorAgreement("code-859", "label-25")
        try {
            item.moveTo(RollingEmissionFactorAgreementStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingEmissionFactorAgreementStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRollingEmissionFactorAgreement("code-859", "label-25")
        item.status = RollingEmissionFactorAgreementStatus.RETIRED
        assertFails("move") { item.moveTo(RollingEmissionFactorAgreementStatus.CREATED) }
    }
}
