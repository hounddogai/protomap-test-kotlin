package com.acme.transport.customs.declaration.exception

import kotlin.test.Test
import kotlin.test.assertTrue

class DeclarationScheduleEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDeclarationScheduleEngine()
        val item = newDeclarationSchedule("code-507", "name-392")
        val result = engine.evaluate(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
