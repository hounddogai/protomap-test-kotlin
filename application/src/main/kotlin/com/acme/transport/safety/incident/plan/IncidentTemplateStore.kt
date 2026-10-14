package com.acme.transport.safety.incident.plan

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.safety.cache.TtlCache
import com.acme.transport.safety.cache.newTtlCache
import com.acme.transport.safety.clock.Clock

/** Keeps incident templates by code. */
interface IncidentTemplateStore {
    fun find(code: String): IncidentTemplate?
    fun save(item: IncidentTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<IncidentTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIncidentTemplateStore(
    var items: MutableMap<String, IncidentTemplate>,
    var order: MutableList<String>,
) : IncidentTemplateStore {
    override fun find(code: String): IncidentTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IncidentTemplate) {
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

    override fun list(): MutableList<IncidentTemplate> {
        val out = mutableListOf<IncidentTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIncidentTemplateStore(): IncidentTemplateStore {
    return MemoryIncidentTemplateStore(mutableMapOf<String, IncidentTemplate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIncidentTemplateStore(
    var inner: IncidentTemplateStore,
    var cache: TtlCache<IncidentTemplate>,
    var hits: Int,
    var misses: Int,
) : IncidentTemplateStore {
    override fun find(code: String): IncidentTemplate? {
        val hit = (cache.get(code) as IncidentTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IncidentTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IncidentTemplate> = inner.list()
}

fun newCachedIncidentTemplateStore(inner: IncidentTemplateStore, ttl: Int, clock: Clock): IncidentTemplateStore {
    return CachedIncidentTemplateStore(
        inner = inner,
        cache = newTtlCache<IncidentTemplate>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects incident templates. Empty fields match all. */
data class IncidentTemplateQuery(
    var title: String,
    var minUpdatedAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IncidentTemplate): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.updatedAt < minUpdatedAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<IncidentTemplate>): MutableList<IncidentTemplate> {
        val matched = filter(items) { candidate: IncidentTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIncidentTemplates(store: IncidentTemplateStore, query: IncidentTemplateQuery): MutableList<IncidentTemplate> {
    return query.apply(store.list())
}
