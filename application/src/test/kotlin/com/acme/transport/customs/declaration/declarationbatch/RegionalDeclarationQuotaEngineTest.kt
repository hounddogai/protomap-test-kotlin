package com.acme.transport.customs.declaration.declarationbatch

import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalDeclarationQuotaEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultRegionalDeclarationQuotaEngine()
        val item = newRegionalDeclarationQuota("code-994", "label-527")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
