package com.acme.transport.network.servicearea.areabatch

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedServiceAreaAgreementEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedServiceAreaAgreementEngine(newRegistry())
        val item = newWeightedServiceAreaAgreement("code-450", "name-321")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
