package com.acme.transport.customs.declaration.plan

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps regional declaration templates by code. */
interface RegionalDeclarationTemplateStore {
    fun find(code: String): RegionalDeclarationTemplate?
    fun save(item: RegionalDeclarationTemplate)
    fun remove(code: String): Boolean
    fun list(): MutableList<RegionalDeclarationTemplate>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryRegionalDeclarationTemplateStore(
    var items: MutableMap<String, RegionalDeclarationTemplate>,
    var order: MutableList<String>,
) : RegionalDeclarationTemplateStore {
    override fun find(code: String): RegionalDeclarationTemplate? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: RegionalDeclarationTemplate) {
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

    override fun list(): MutableList<RegionalDeclarationTemplate> {
        val out = mutableListOf<RegionalDeclarationTemplate>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryRegionalDeclarationTemplateStore(): RegionalDeclarationTemplateStore {
    return MemoryRegionalDeclarationTemplateStore(
        mutableMapOf<String, RegionalDeclarationTemplate>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedRegionalDeclarationTemplateStore(
    var inner: RegionalDeclarationTemplateStore,
    var cache: TtlCache<RegionalDeclarationTemplate>,
    var hits: Int,
    var misses: Int,
) : RegionalDeclarationTemplateStore {
    override fun find(code: String): RegionalDeclarationTemplate? {
        val hit = (cache.get(code) as RegionalDeclarationTemplate?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: RegionalDeclarationTemplate) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<RegionalDeclarationTemplate> = inner.list()
}

fun newCachedRegionalDeclarationTemplateStore(inner: RegionalDeclarationTemplateStore, ttl: Int, clock: Clock): RegionalDeclarationTemplateStore {
    return CachedRegionalDeclarationTemplateStore(
        inner = inner,
        cache = newTtlCache<RegionalDeclarationTemplate>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects regional declaration templates. Empty fields match all. */
data class RegionalDeclarationTemplateQuery(
    var label: String,
    var minQuantity: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: RegionalDeclarationTemplate): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.quantity < minQuantity) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<RegionalDeclarationTemplate>): MutableList<RegionalDeclarationTemplate> {
        val matched = filter(items) { candidate: RegionalDeclarationTemplate -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findRegionalDeclarationTemplates(store: RegionalDeclarationTemplateStore, query: RegionalDeclarationTemplateQuery): MutableList<RegionalDeclarationTemplate> {
    return query.apply(store.list())
}
