package com.acme.transport.customs.declaration.declarationforecast

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DeclarationClaimEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDeclarationClaimEngine(newRegistry())
        val item = newDeclarationClaim("code-283", "name-141")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
