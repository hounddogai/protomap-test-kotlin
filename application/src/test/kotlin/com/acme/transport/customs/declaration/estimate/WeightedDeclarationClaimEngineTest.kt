package com.acme.transport.customs.declaration.estimate

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class WeightedDeclarationClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultWeightedDeclarationClaimEngine(newRegistry())
        val item = newWeightedDeclarationClaim("code-858", "label-957")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
