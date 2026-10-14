package com.acme.transport.lastmile.locker.cycle

import kotlin.test.Test
import kotlin.test.assertTrue

class LockerTemplateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLockerTemplateEngine()
        val item = newLockerTemplate("code-541", "name-825")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
