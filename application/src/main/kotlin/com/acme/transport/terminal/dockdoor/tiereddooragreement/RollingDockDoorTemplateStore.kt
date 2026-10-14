package com.acme.transport.terminal.dockdoor.tiereddooragreement

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps rolling dock door templates by code. */
interface RollingDockDoorTemplateStore {
    fun find(code: String): RollingDockDoorTemplate?
    fun save(item: RollingDockDoorTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingDockDoorTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingDockDoorTemplateStore(
    var items: MutableMap<String, RollingDockDoorTemplate>,
    var order: MutableList<String>,
) : RollingDockDoorTemplateStore {
    override fun find(code: String): RollingDockDoorTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingDockDoorTemplate) {
        if (!items.containsKey(item.code)) {
            order.add(item.code)
        }
        items[item.code] = item
    }

    override fun remove(code: String): Boolean {
        if (!items.containsKey(code)) {
            return false
        }
        items.remove(code)
        val kept = mutableListOf<String>()
        for (existing in order) {
            if (existing != code) {
                kept.add(existing)
            }
        }
        order = kept
        return true
    }

    override fun list(): MutableList<RollingDockDoorTemplate> {
        val out = mutableListOf<RollingDockDoorTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingDockDoorTemplateStore(): RollingDockDoorTemplateStore {
    return MemoryRollingDockDoorTemplateStore(
        mutableMapOf<String, RollingDockDoorTemplate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingDockDoorTemplateStore(
    var inner: RollingDockDoorTemplateStore,
    var cache: TtlCache<RollingDockDoorTemplate>,
    var hits: Int,
    var misses: Int,
) : RollingDockDoorTemplateStore {
    override fun find(code: String): RollingDockDoorTemplate? {
        val hit = (cache.get(code) as RollingDockDoorTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingDockDoorTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingDockDoorTemplate> = inner.list()
}

fun newCachedRollingDockDoorTemplateStore(inner: RollingDockDoorTemplateStore, ttl: Int, clock: Clock): RollingDockDoorTemplateStore {
    return CachedRollingDockDoorTemplateStore(
        inner = inner,
        cache = newTtlCache<RollingDockDoorTemplate>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling dock door templates. Empty fields match all. */
data class RollingDockDoorTemplateQuery(
    var country: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingDockDoorTemplate): Boolean {
        if (country != "" && normalize(item.country) != normalize(country)) {
            return false
        }
        if (item.durationMinutes < minDurationMinutes) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingDockDoorTemplate>): MutableList<RollingDockDoorTemplate> {
        val matched = filter(items) { candidate: RollingDockDoorTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingDockDoorTemplates(store: RollingDockDoorTemplateStore, query: RollingDockDoorTemplateQuery): MutableList<RollingDockDoorTemplate> {
    return query.apply(store.list())
}
