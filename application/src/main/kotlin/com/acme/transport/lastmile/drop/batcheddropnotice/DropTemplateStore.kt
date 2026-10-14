package com.acme.transport.lastmile.drop.batcheddropnotice

import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize
import com.acme.transport.lastmile.cache.TtlCache
import com.acme.transport.lastmile.cache.newTtlCache
import com.acme.transport.lastmile.clock.Clock

/** Keeps drop templates by code. */
interface DropTemplateStore {
    fun find(code: String): DropTemplate?
    fun save(item: DropTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<DropTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDropTemplateStore(
    var items: MutableMap<String, DropTemplate>,
    var order: MutableList<String>,
) : DropTemplateStore {
    override fun find(code: String): DropTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DropTemplate) {
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

    override fun list(): MutableList<DropTemplate> {
        val out = mutableListOf<DropTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDropTemplateStore(): DropTemplateStore {
    return MemoryDropTemplateStore(mutableMapOf<String, DropTemplate>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDropTemplateStore(
    var inner: DropTemplateStore,
    var cache: TtlCache<DropTemplate>,
    var hits: Int,
    var misses: Int,
) : DropTemplateStore {
    override fun find(code: String): DropTemplate? {
        val hit = (cache.get(code) as DropTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DropTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DropTemplate> = inner.list()
}

fun newCachedDropTemplateStore(inner: DropTemplateStore, ttl: Int, clock: Clock): DropTemplateStore {
    return CachedDropTemplateStore(
        inner = inner,
        cache = newTtlCache<DropTemplate>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects drop templates. Empty fields match all. */
data class DropTemplateQuery(
    var channel: String,
    var minWeight: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DropTemplate): Boolean {
        if (channel != "" && normalize(item.channel) != normalize(channel)) {
            return false
        }
        if (item.weight < minWeight) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DropTemplate>): MutableList<DropTemplate> {
        val matched = filter(items) { candidate: DropTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDropTemplates(store: DropTemplateStore, query: DropTemplateQuery): MutableList<DropTemplate> {
    return query.apply(store.list())
}
