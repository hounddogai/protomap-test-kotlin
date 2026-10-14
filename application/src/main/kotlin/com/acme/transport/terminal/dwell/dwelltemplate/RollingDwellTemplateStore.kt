package com.acme.transport.terminal.dwell.dwelltemplate

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.terminal.cache.TtlCache
import com.acme.transport.terminal.cache.newTtlCache
import com.acme.transport.terminal.clock.Clock

/** Keeps rolling dwell templates by code. */
interface RollingDwellTemplateStore {
    fun find(code: String): RollingDwellTemplate?
    fun save(item: RollingDwellTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<RollingDwellTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRollingDwellTemplateStore(
    var items: MutableMap<String, RollingDwellTemplate>,
    var order: MutableList<String>,
) : RollingDwellTemplateStore {
    override fun find(code: String): RollingDwellTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RollingDwellTemplate) {
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

    override fun list(): MutableList<RollingDwellTemplate> {
        val out = mutableListOf<RollingDwellTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRollingDwellTemplateStore(): RollingDwellTemplateStore {
    return MemoryRollingDwellTemplateStore(
        mutableMapOf<String, RollingDwellTemplate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRollingDwellTemplateStore(
    var inner: RollingDwellTemplateStore,
    var cache: TtlCache<RollingDwellTemplate>,
    var hits: Int,
    var misses: Int,
) : RollingDwellTemplateStore {
    override fun find(code: String): RollingDwellTemplate? {
        val hit = (cache.get(code) as RollingDwellTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RollingDwellTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RollingDwellTemplate> = inner.list()
}

fun newCachedRollingDwellTemplateStore(inner: RollingDwellTemplateStore, ttl: Int, clock: Clock): RollingDwellTemplateStore {
    return CachedRollingDwellTemplateStore(
        inner = inner,
        cache = newTtlCache<RollingDwellTemplate>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects rolling dwell templates. Empty fields match all. */
data class RollingDwellTemplateQuery(
    var depot: String,
    var minScore: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RollingDwellTemplate): Boolean {
        if (depot != "" && normalize(item.depot) != normalize(depot)) {
            return false
        }
        if (item.score < minScore) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RollingDwellTemplate>): MutableList<RollingDwellTemplate> {
        val matched = filter(items) { candidate: RollingDwellTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRollingDwellTemplates(store: RollingDwellTemplateStore, query: RollingDwellTemplateQuery): MutableList<RollingDwellTemplate> {
    return query.apply(store.list())
}
