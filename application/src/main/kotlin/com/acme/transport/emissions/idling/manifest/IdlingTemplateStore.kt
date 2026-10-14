package com.acme.transport.emissions.idling.manifest

import com.acme.transport.emissions.cache.TtlCache
import com.acme.transport.emissions.cache.newTtlCache
import com.acme.transport.emissions.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps idling templates by code. */
interface IdlingTemplateStore {
    fun find(code: String): IdlingTemplate?
    fun save(item: IdlingTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<IdlingTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryIdlingTemplateStore(
    var items: MutableMap<String, IdlingTemplate>,
    var order: MutableList<String>,
) : IdlingTemplateStore {
    override fun find(code: String): IdlingTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: IdlingTemplate) {
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

    override fun list(): MutableList<IdlingTemplate> {
        val out = mutableListOf<IdlingTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryIdlingTemplateStore(): IdlingTemplateStore {
    return MemoryIdlingTemplateStore(mutableMapOf<String, IdlingTemplate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedIdlingTemplateStore(
    var inner: IdlingTemplateStore,
    var cache: TtlCache<IdlingTemplate>,
    var hits: Int,
    var misses: Int,
) : IdlingTemplateStore {
    override fun find(code: String): IdlingTemplate? {
        val hit = (cache.get(code) as IdlingTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: IdlingTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<IdlingTemplate> = inner.list()
}

fun newCachedIdlingTemplateStore(inner: IdlingTemplateStore, ttl: Int, clock: Clock): IdlingTemplateStore {
    return CachedIdlingTemplateStore(
        inner = inner,
        cache = newTtlCache<IdlingTemplate>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects idling templates. Empty fields match all. */
data class IdlingTemplateQuery(
    var label: String,
    var minDurationMinutes: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: IdlingTemplate): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<IdlingTemplate>): MutableList<IdlingTemplate> {
        val matched = filter(items) { candidate: IdlingTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findIdlingTemplates(store: IdlingTemplateStore, query: IdlingTemplateQuery): MutableList<IdlingTemplate> {
    return query.apply(store.list())
}
