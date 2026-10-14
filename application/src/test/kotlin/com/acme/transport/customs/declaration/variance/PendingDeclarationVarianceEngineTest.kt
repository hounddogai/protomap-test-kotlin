package com.acme.transport.customs.declaration.variance

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingDeclarationVarianceEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingDeclarationVarianceEngine(newRegistry())
        val item = newPendingDeclarationVariance("code-382", "label-118")
        val result = engine.price(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
