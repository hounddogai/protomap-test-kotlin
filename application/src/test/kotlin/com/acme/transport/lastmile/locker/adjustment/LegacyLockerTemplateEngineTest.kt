package com.acme.transport.lastmile.locker.adjustment

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyLockerTemplateEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyLockerTemplateEngine(newRegistry())
        val item = newLegacyLockerTemplate("code-583", "title-56")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
