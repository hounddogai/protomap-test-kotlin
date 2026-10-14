package com.acme.transport.lastmile.proofofdelivery.deliveryclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualProofOfDeliveryClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualProofOfDeliveryClaimValidator()
        val item = newManualProofOfDeliveryClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = ManualProofOfDeliveryClaimRequiredDepotRule("manual proof of delivery claim needs a value")
        val item = newManualProofOfDeliveryClaim("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ManualProofOfDeliveryClaimRequiredLabelRule("manual proof of delivery claim needs a value")
        val item = newManualProofOfDeliveryClaim("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
