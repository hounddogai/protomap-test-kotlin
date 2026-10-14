package com.acme.transport.customs.declaration.plan

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DeclarationPlanEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDeclarationPlanEngine(newRegistry())
        val item = newDeclarationPlan("code-341", "name-599")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
