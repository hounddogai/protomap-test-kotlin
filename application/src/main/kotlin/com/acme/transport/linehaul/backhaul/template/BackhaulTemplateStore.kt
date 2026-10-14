package com.acme.transport.linehaul.backhaul.template

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.linehaul.cache.TtlCache
import com.acme.transport.linehaul.cache.newTtlCache
import com.acme.transport.linehaul.clock.Clock

/** Keeps backhaul templates by code. */
interface BackhaulTemplateStore {
    fun find(code: String): BackhaulTemplate?
    fun save(item: BackhaulTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<BackhaulTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryBackhaulTemplateStore(
    var items: MutableMap<String, BackhaulTemplate>,
    var order: MutableList<String>,
) : BackhaulTemplateStore {
    override fun find(code: String): BackhaulTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: BackhaulTemplate) {
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

    override fun list(): MutableList<BackhaulTemplate> {
        val out = mutableListOf<BackhaulTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryBackhaulTemplateStore(): BackhaulTemplateStore {
    return MemoryBackhaulTemplateStore(mutableMapOf<String, BackhaulTemplate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedBackhaulTemplateStore(
    var inner: BackhaulTemplateStore,
    var cache: TtlCache<BackhaulTemplate>,
    var hits: Int,
    var misses: Int,
) : BackhaulTemplateStore {
    override fun find(code: String): BackhaulTemplate? {
        val hit = (cache.get(code) as BackhaulTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: BackhaulTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<BackhaulTemplate> = inner.list()
}

fun newCachedBackhaulTemplateStore(inner: BackhaulTemplateStore, ttl: Int, clock: Clock): BackhaulTemplateStore {
    return CachedBackhaulTemplateStore(
        inner = inner,
        cache = newTtlCache<BackhaulTemplate>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects backhaul templates. Empty fields match all. */
data class BackhaulTemplateQuery(
    var name: String,
    var minSequence: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: BackhaulTemplate): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.sequence < minSequence) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<BackhaulTemplate>): MutableList<BackhaulTemplate> {
        val matched = filter(items) { candidate: BackhaulTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findBackhaulTemplates(store: BackhaulTemplateStore, query: BackhaulTemplateQuery): MutableList<BackhaulTemplate> {
    return query.apply(store.list())
}
